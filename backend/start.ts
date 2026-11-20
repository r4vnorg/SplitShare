import { DatabaseSync } from 'node:sqlite';
import express from 'express';
import bodyParser from 'body-parser';
import { randomUUID } from 'node:crypto';

/* Types */
interface Dictionary<T> {
    [key: string] : T
}

interface CachedResponse {
    status: number
    body: any
}

/* Helpers */
const sendDbError = (res: express.Response, err: unknown, fallbackStatus: number = 500, fallbackMessage: string = 'Database error') => {
    
    if (err instanceof Error && 'errstr' in err) {
        const sqliteErr = err as Error & { errstr: string; errcode: number };
        const isConstraint = sqliteErr.errstr?.toLowerCase().includes('constraint');
        const status = isConstraint ? 409 : fallbackStatus;
        const message = sqliteErr.message || fallbackMessage;
        console.error(`DB error (code ${sqliteErr.errcode}):`, message);
        return res.status(status).send(message);
    }
    console.error('Unexpected error:', err);
    return res.status(fallbackStatus).send(fallbackMessage);
};

/**
 * Idempotency wrapper: if a response for this request_id already exists,
 * return it immediately without executing the callback.
 */
const withIdempotency = (
    req: express.Request,
    res: express.Response,
    callback: () => { status: number, body: any }
) => {
    const requestId = req.query.request_id as string | undefined;
    if (!requestId) {
        return res.status(400).send('request_id query parameter is not present, required for reliability');
    }

    const cached = responses[requestId];
    if (cached) {
        console.log(`[idempotent] returning cached response for request_id=${requestId}`);
        return res.status(cached.status).send(cached.body);
    }

    const result = callback();
    responses[requestId] = result;
    return res.status(result.status).send(result.body);
};


/* Consts */ 
const app = express()
app.use(bodyParser.json())
const db = new DatabaseSync('db.sqlite') 
const responses: Dictionary<CachedResponse> = {}

// ── Auth ──────────────────────────────────────────────────────────────
app.post('/login', (req, res) => {
    console.log(req.method, req.url)

    /* Auth requests are not idempotents, do not care about consistency */
    const { email } = req.body

    let query
    try {
        query = db.prepare(`SELECT * FROM people WHERE email = ?`).get(email)
    } catch (err) {
        return sendDbError(res, err, 404, 'User account not found')
    }

    if (typeof query === 'undefined')
        return res.status(404).send('User account not found')

    res.status(200).send(query)
})

app.post('/register', (req, res) => {
    console.log(req.method, req.url)

    const { name, email, phone } = req.body
    const id = randomUUID()

    try {

        db.prepare("INSERT INTO people(id, name, email, phone) VALUES (?, ?, ?, ?)")
            .run(id, name, email, phone)

    } catch(err) {
        return sendDbError(res, err, 400, 'Unable to create user account')
    }

    return res.status(201).send({id: id, name: name, email: email, phone: phone})
})

// ── People (directory) ─────────────────────────────────────────────────

app.get('/people', (req, res) => {
    console.log(req.method, req.url)

    let query
    try {
        query = db.prepare(`SELECT * FROM people`).all()
    } catch (err) {
        return sendDbError(res, err, 500, 'Failed to fetch people');
    }
    res.status(200).send(query)
})

app.get('/people/:id', (req, res) => {
    console.log(req.method, req.url)

    let query;
    try {
        query = db.prepare(`SELECT * FROM people WHERE id = ?`).get(req.params.id)
    } catch (err) {
        return sendDbError(res, err, 500, `Failed to fetch person ${req.params.id}`);
    }

    if (typeof query === 'undefined')
        res.status(404).send(`Person with id ${req.params.id} not found`)

    res.status(200).send(query)
})

app.post('/people', (req, res) => {
    console.log(req.method, req.url);

    withIdempotency(req, res, () => {
        const { id, name, email, phone } = req.body
        try {
            db.prepare(`INSERT INTO people (id, name, email, phone) VALUES (?, ?, ?, ?)`).run(id, name, email, phone)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to create person`);
            return { status: 500, body: null }
        }
        return { status: 201, body: { id, name, email, phone } }
    })
})

app.put('/people/:id', (req, res) => {
    console.log(req.method, req.url);

    withIdempotency(req, res, () => {
        const { name, email, phone } = req.body
        try {
            db.prepare(`UPDATE people SET name = ?, email = ?, phone = ? WHERE id = ?`).run(name, email, phone, req.params.id)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to update person ${req.params.id}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { id: req.params.id, name, email, phone } }
    })
})

app.delete('/people/:id', (req, res) => {
    console.log(req.method, req.url);

    withIdempotency(req, res, () => {
        try {
            db.prepare(`DELETE FROM people WHERE id = ?`).run(req.params.id)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to delete person ${req.params.id}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { deleted: req.params.id } }
    })
})

// ── People → Groups ────────────────────────────────────────────────────

app.get('/people/:id/groups', (req, res) => {
    console.log(req.method, req.url)

    try {
        if (typeof db.prepare(`SELECT * FROM people WHERE id = ?`).get(req.params.id) === 'undefined')
            return res.status(404).send("User not found")

        const groups = db.prepare(`
            SELECT g.id, g.name, g.description, g.createdAt,
                   (SELECT COUNT(*) FROM participants p2
                    WHERE p2.groupId = g.id AND p2.isActive = 1) AS participantCount
            FROM groups g
            INNER JOIN participants p ON p.groupId = g.id
            WHERE p.personId = ?
        `).all(req.params.id)

        const participants = db.prepare(`
            SELECT * FROM participants
            WHERE groupId IN (SELECT g.id FROM groups g INNER JOIN participants p ON p.groupId = g.id WHERE p.personId = ?)
        `).all(req.params.id)

        const result = (groups as any[]).map((group: any) => ({
            group: {
                id: group.id,
                name: group.name,
                description: group.description,
                createdAt: group.createdAt,
            },
            participants: (participants as any[]).filter((p: any) => p.groupId === group.id).map((p: any) => ({
                groupId: p.groupId,
                personId: p.personId,
                addedAt: p.addedAt,
                removedAt: null,
                syncStatus: 'SYNCED',
            })),
        }))

        res.status(200).send(result)
    } catch (err) {
        return sendDbError(res, err, 500, `Failed to fetch groups for person ${req.params.id}`);
    }
})

// ── Groups ─────────────────────────────────────────────────────────────

app.get('/groups', (req, res) => {
    console.log(req.method, req.url)

    let query;
    try {
        query = db.prepare(`SELECT * FROM groups`).all()
    } catch (err) {
        return sendDbError(res, err, 500, 'Failed to fetch groups');
    }
    res.status(200).send(query)
})

app.get('/groups/:id', (req, res) => {
    console.log(req.method, req.url)

    let query;
    try {
        query = db.prepare(`SELECT * FROM groups WHERE id = ?`).get(req.params.id)
    } catch (err) {
        return sendDbError(res, err, 500, `Failed to fetch group ${req.params.id}`);
    }

    if (typeof query === 'undefined')
        res.status(404).send(`Group with id ${req.params.id} not found`)

    res.status(200).send(query)
})

app.post('/groups', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        const uuid = randomUUID()
        const timestamp = Date.now()
        const { name, description, creatorID } = req.body

        try {
            db.prepare(`INSERT INTO groups (id, name, description, createdAt) VALUES (?, ?, ?, ?)`).run(uuid, name, description ?? '', timestamp)
            db.prepare(`INSERT INTO participants (groupId, personId, isActive, addedAt) VALUES (?, ?, ?, ?)`).run(uuid, creatorID, 1, timestamp)
        } catch (err) {
            return { status: 500, body: `Failed to create group` }
        }
        return { status: 201, body: {
            group: { id: uuid, name: name, description: description, createdAt: timestamp },
            participants: [{
                groupId: uuid,
                personId: creatorID,
                addedAt: timestamp,
                removedAt: null,
                syncStatus: 'SYNCED',
            }],
        }}
    })
})

app.put('/groups/:id', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        const { name, description, createdAt } = req.body
        try {
            db.prepare(`UPDATE groups SET name = ?, description = ?, createdAt = ? WHERE id = ?`).run(name, description, createdAt, req.params.id)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to update group ${req.params.id}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { id: req.params.id, name, description, createdAt } }
    })
})

app.delete('/groups/:id', (req, res) => {
    console.log( req.method, req.url);

    withIdempotency(req, res, () => {
        try {
            db.prepare(`DELETE FROM groups WHERE id = ?`).run(req.params.id)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to delete group ${req.params.id}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { deleted: req.params.id } }
    })
})

// ── Participants ───────────────────────────────────────────────────────

app.get('/groups/:groupId/participants', (req, res) => {
    console.log(req.method, req.url)

    try {
        const rows = db.prepare(`
            SELECT p.groupId, p.personId, p.isActive, p.addedAt, pe.id, pe.name, pe.email, pe.phone
            FROM participants p
            JOIN people pe ON pe.id = p.personId
            WHERE p.groupId = ?
        `).all(req.params.groupId)

        const result = (rows as any[]).map((r: any) => ({
            participant: {
                groupId: r.groupId,
                personId: r.personId,
                addedAt: r.addedAt,
                removedAt: null,
                syncStatus: 'SYNCED',
            },
            person: { id: r.id, name: r.name, email: r.email, phone: r.phone },
        }))

        res.status(200).send(result)
    } catch (err) {
        return sendDbError(res, err, 500, `Failed to fetch participants for group ${req.params.groupId}`);
    }
})

app.post('/groups/:groupId/participants', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        const { email } = req.body
        const person = db.prepare(`SELECT * FROM people WHERE email = ?`).get(email)
        if (typeof person === 'undefined') {
            return { status: 404, body: `Person with email ${email} not found` }
        }
        const personId = person.id
        const addedAt = Date.now()
        try {
            db.prepare(`INSERT INTO participants (groupId, personId, isActive, addedAt) VALUES (?, ?, ?, ?)`)
                .run(req.params.groupId, personId, 1, addedAt)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to add participant to group ${req.params.groupId}`);
            return { status: 500, body: null }
        }
        return { status: 201, body: {
            participant: {
                groupId: req.params.groupId,
                personId,
                addedAt,
                removedAt: null,
                syncStatus: 'SYNCED',
            },
            person: { id: person.id, name: person.name, email: person.email, phone: person.phone },
        }}
    })
})

app.delete('/groups/:groupId/participants/:personId', (req, res) => {
    console.log( req.method, req.url)

    withIdempotency(req, res, () => {
        try {
            db.prepare(`DELETE FROM participants WHERE groupId = ? AND personId = ?`).run(req.params.groupId, req.params.personId)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to remove participant from group ${req.params.groupId}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { deleted: { groupId: req.params.groupId, personId: req.params.personId } } }
    })
})

// ── Expenses ───────────────────────────────────────────────────────────

app.get('/groups/:groupId/expenses', (req, res) => {
    console.log( req.method, req.url)

    let query;
    try {
        query = db.prepare(`SELECT * FROM expenses WHERE groupId = ?`).all(req.params.groupId)
    } catch (err) {
        return sendDbError(res, err, 500, `Failed to fetch expenses for group ${req.params.groupId}`);
    }
    res.status(200).send(query)
})

app.get('/expenses/:id', (req, res) => {
    console.log(req.method, req.url)

    let query;
    try {
        query = db.prepare(`SELECT * FROM expenses WHERE id = ?`).get(req.params.id)
    } catch (err) {
        return sendDbError(res, err, 500, `Failed to fetch expense ${req.params.id}`);
    }

    if (typeof query === 'undefined')
        res.status(404).send(`Expense with id ${req.params.id} not found`)

    res.status(200).send(query)
})

app.post('/expenses', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        const { id, groupId, description, amountMinor, payerId, createdAt, syncStatus, splits } = req.body
        try {
            db.prepare(`INSERT INTO expenses (id, groupId, description, amountMinor, payerId, createdAt, syncStatus, splits) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`)
                .run(id, groupId, description, amountMinor, payerId, createdAt, syncStatus, JSON.stringify(splits))
        } catch (err) {
            sendDbError(res, err, 500, `Failed to create expense`);
            return { status: 500, body: null }
        }
        return { status: 201, body: { id, groupId, description, amountMinor, payerId, createdAt, syncStatus, splits } }
    })
})

app.put('/expenses/:id', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        const { groupId, description, amountMinor, payerId, createdAt, syncStatus, splits } = req.body
        try {
            db.prepare(`UPDATE expenses SET groupId = ?, description = ?, amountMinor = ?, payerId = ?, createdAt = ?, syncStatus = ?, splits = ? WHERE id = ?`)
                .run(groupId, description, amountMinor, payerId, createdAt, syncStatus, JSON.stringify(splits), req.params.id)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to update expense ${req.params.id}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { id: req.params.id, groupId, description, amountMinor, payerId, createdAt, syncStatus, splits } }
    })
})

app.delete('/expenses/:id', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        try {
            db.prepare(`DELETE FROM expenses WHERE id = ?`).run(req.params.id)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to delete expense ${req.params.id}`);
            return { status: 500, body: null }
        }
        return { status: 200, body: { deleted: req.params.id } }
    })
})

// ── Reminders ──────────────────────────────────────────────────────────

app.post('/groups/:groupId/reminders', (req, res) => {
    console.log(req.method, req.url)

    withIdempotency(req, res, () => {
        const { toPersonId, amountMinor } = req.body
        try {
            db.prepare(`INSERT INTO reminders (groupId, toPersonId, amountMinor) VALUES (?, ?, ?)`)
                .run(req.params.groupId, toPersonId, amountMinor)
        } catch (err) {
            sendDbError(res, err, 500, `Failed to send reminder for group ${req.params.groupId}`);
            return { status: 500, body: null }
        }
        return { status: 201, body: { groupId: req.params.groupId, toPersonId, amountMinor } }
    })
})





app.listen(8080, '0.0.0.0', () => {
    
    console.log(`Server is listening at http://0.0.0.0:${8080}`);
});

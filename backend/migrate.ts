import { DatabaseSync } from 'node:sqlite'

const database = new DatabaseSync('db.sqlite')


/* Utilities */
const tableQuery = database.prepare(`SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?`)
const dropIfExists = (name: string) => {

    const result = tableQuery.get(name)
    if (typeof result === 'undefined') {
        return; //Table does not exist
    }
    
    console.log(`Removing table ${name}...`)
    database.exec(`DROP TABLE ${name}`)
} 

const createTable = (name: string, fields: string) => {

    const result = tableQuery.get(name)
    if (typeof result !== 'undefined') {
        console.warn(`Skipping creating table ${name}: Already exists!`)
        return;
    }

    console.log(`Creating table ${name}...`)
    database.exec(`CREATE TABLE ${name} (${fields}) STRICT`)
}
/* Tables */
const migratePeople = () => {
    dropIfExists('people')
    createTable('people', 'id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, phone TEXT')
}

const migrateGroups = () => {
    dropIfExists('groups')
    createTable('groups', 'id TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT, createdAt INTEGER NOT NULL')
}

const migrateParticipants = () => {
    dropIfExists('participants')
    createTable('participants', 'groupId TEXT NOT NULL, personId TEXT NOT NULL, isActive INTEGER NOT NULL DEFAULT 1, addedAt INTEGER NOT NULL, PRIMARY KEY (groupId, personId)')
}

const migrateExpenses = () => {
    dropIfExists('expenses')
    createTable('expenses', 'id TEXT PRIMARY KEY, groupId TEXT NOT NULL, description TEXT NOT NULL, amountMinor INTEGER NOT NULL, payerId TEXT NOT NULL, createdAt INTEGER NOT NULL, syncStatus TEXT NOT NULL, splits TEXT NOT NULL')
}

const migrateReminders = () => {
    dropIfExists('reminders')
    createTable('reminders', 'id TEXT PRIMARY KEY, groupId TEXT NOT NULL, toPersonId TEXT NOT NULL, amountMinor INTEGER NOT NULL')
}


/* Doing the migrations */
migratePeople()
migrateGroups()
migrateParticipants()
migrateExpenses()
migrateReminders()

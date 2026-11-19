import { DatabaseSync } from 'node:sqlite'

const db = new DatabaseSync('db.sqlite')

/* ── Table metadata ───────────────────────────────────────────── */
interface TableInfo {
    name: string
    columns: string[]
}

const tables: TableInfo[] = [
    { name: 'people', columns: ['id', 'name', 'email', 'phone'] },
    { name: 'groups', columns: ['id', 'name', 'description', 'createdAt'] },
    { name: 'participants', columns: ['groupId', 'personId', 'person', 'isActive'] },
    { name: 'expenses', columns: ['id', 'groupId', 'description', 'amountMinor', 'payerId', 'createdAt', 'syncStatus', 'splits'] },
    { name: 'reminders', columns: ['id', 'groupId', 'toPersonId', 'amountMinor'] },
]

/* ── Formatting helpers ───────────────────────────────────────── */

/** Convert a raw value to a display string */
function formatValue(value: unknown, column: string): string {
    if (value === null || value === undefined) return ''

    // Format timestamps as ISO dates
    if (column === 'createdAt' && typeof value === 'number') {
        return new Date(value).toISOString()
    }

    // Format currency (minor units -> major with 2 decimals)
    if (column === 'amountMinor' && typeof value === 'number') {
        return (value / 100).toFixed(2)
    }

    // Format JSON arrays/objects
    if (typeof value === 'string' && (column === 'splits' || column === 'person')) {
        try {
            return JSON.stringify(JSON.parse(value), null, 2)
        } catch {
            return value
        }
    }

    // Boolean-ish integers
    if (column === 'isActive' && typeof value === 'number') {
        return value === 1 ? 'true' : 'false'
    }

    return String(value)
}

/** Calculate column widths for a table */
function calcWidths(rows: Record<string, unknown>[], columns: string[]): number[] {
    const widths = columns.map(col => col.length)
    for (const row of rows) {
        for (const col of columns) {
            widths[columns.indexOf(col)] = Math.max(widths[columns.indexOf(col)], formatValue(row[col], col).length)
        }
    }
    return widths
}

/** Print a single table */
function printTable(name: string, rows: Record<string, unknown>[], columns: string[]) {
    if (rows.length === 0) {
        console.log(`\n\`\`\`${name}\n(0 rows)\n\`\`\``)
        return
    }

    const widths = calcWidths(rows, columns)

    // Build separator line
    const sep = '+' + widths.map(w => '-'.repeat(w + 2)).join('+') + '+'

    // Header
    console.log(`\n\`\`\`${name} (${rows.length} rows)`)
    console.log(sep)
    console.log('| ' + columns.map((col, i) => ` ${col.padEnd(widths[i])} `).join('|') + '|')
    console.log(sep)

    // Rows
    for (const row of rows) {
        console.log('| ' + columns.map((col, i) => ` ${formatValue(row[col], col).padEnd(widths[i])} `).join('|') + '|')
    }

    console.log(sep)
    console.log('```')
}

/* ── Main ─────────────────────────────────────────────────────── */
console.log('='.repeat(60))
console.log('  SplitShare Database Dump')
console.log(`  ${new Date().toISOString()}`)
console.log('='.repeat(60))

for (const table of tables) {
    const rows = db.prepare(`SELECT * FROM ${table.name}`).all()
    printTable(table.name, rows, table.columns)
}

db.close()

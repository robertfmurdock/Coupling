// Kotlin's ESM export exposes the reporter singleton through getInstance().
const { MochaLoggingReporter } = require('./testmints-mint-logs.mjs')
module.exports = { MochaLoggingReporter: MochaLoggingReporter.getInstance() }

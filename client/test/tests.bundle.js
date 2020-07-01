require('Coupling-client-test').com.zegreatrob.coupling.client
  .setLogLevel();

require('./enzymeConfig');

const context = require.context('.', true, /.+\.spec\*?$/);
context.keys().forEach(context);

module.exports = context;
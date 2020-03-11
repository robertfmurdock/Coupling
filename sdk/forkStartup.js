require('../server/build/executable/app')
  .start()
  .then(() => process.send('ready'));
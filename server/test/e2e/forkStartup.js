require('../../build/executable/app')
  .start()
  .then(() => process.send('ready'));
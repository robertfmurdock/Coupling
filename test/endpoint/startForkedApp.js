const builtApp = require('../../build/app');
builtApp.start()
  .then(function () {
    process.send({message: 'Application Ready'});
  });



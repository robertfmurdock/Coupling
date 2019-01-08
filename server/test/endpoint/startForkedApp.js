const builtApp = require('../../../build/app');

builtApp.start()
  .then(function (server) {
    if (process.send) {
      process.send({message: 'Application Ready'});
    }
    process.on('SIGINT', function () {
      server.close();
      process.exit(1);
    });
  });


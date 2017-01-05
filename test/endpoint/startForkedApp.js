const builtApp = require('../../build/app');
console.log('start forked app');
builtApp.start()
  .then(function (server) {
    if (process.send) {
      process.send({message: 'Application Ready'});
    }
    process.on('SIGINT', function () {
      console.log('exiting forked app.');
      server.close();
      process.exit(1);
    });
  });


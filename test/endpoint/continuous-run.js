const webpackRunner = require('../webpackRunner');
var config = require('./webpack.config');

const runHelpers = require('../run-helpers');
const forkHelpers = require('./../fork-helpers');

function forkJasmine() {
  return forkHelpers.forkJasmine('test/endpoint','.tmp', 'test.js', __dirname + '/../../../test-output', 'endpoint.xml');
}

const removeTempDirectory = function () {
  runHelpers.removeTempDirectory(__dirname + '/.tmp')
};

process.env.PORT = 3001;
require('../../build/app').start()
  .then(function () {
    var testRun = undefined;
    webpackRunner.watch(config, function (err, stats) {
      console.log('stats', stats.toString('minimal'));
      if (!err) {
        if (testRun) {
          testRun = testRun
            .then(forkJasmine, function (err) {
              console.log('Exiting:', err);
              process.exit(-1);
            })
        } else {
          testRun = forkJasmine();
        }

      } else {
        console.log(err);
      }
    });
  });


process.on('SIGINT', function () {
  console.log("Caught interrupt signal");
  removeTempDirectory(__dirname + '/.tmp');
});
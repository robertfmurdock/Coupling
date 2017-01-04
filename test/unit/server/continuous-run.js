const runHelpers = require('./run-helpers');
const webpackRunner = require('../../webpackRunner');
var config = require('./webpack.config');

const removeTempDirectory = runHelpers.removeTempDirectory;

var forkPromise = require('fork-promise');

var testRun = undefined;

function forkJasmine() {
  return forkPromise.fn(function (done) {
    require(__dirname + '/../../../test/unit/server/run-helpers').startJasmine()
      .then(done, function (err) {
        console.log('Exiting fork:', err);
        done(-1);
      })
  });
}

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

process.on('SIGINT', function () {
  console.log("Caught interrupt signal");
  removeTempDirectory();
  process.exit();
});
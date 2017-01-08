const webpackRunner = require('../webpackRunner');
var serverWebpackConfig = require('../../server/webpack.config');
var testWebpackConfig = require('./webpack.config');
const childProcess = require('child_process');
const Promise = require('bluebird');

const runHelpers = require('../run-helpers');
const forkHelpers = require('./../fork-helpers');

function forkJasmine() {
  console.log('fork jasmine');
  return forkHelpers.forkJasmine('test/endpoint', '.tmp', 'test.js', __dirname + '/../../../test-output', 'endpoint.xml');
}

const removeTempDirectory = function () {
  runHelpers.removeTempDirectory(__dirname + '/.tmp')
};

var appProcess;

var appWatcher = webpackRunner.watch(serverWebpackConfig, function () {
  if (!appProcess) {
    appProcess = startForkedAppAndWatchTests();
  } else {
    appProcess.kill('SIGINT');

    appProcess = startForkedAppAndWatchTests();
  }
});

process.env.PORT = 3001;

var startForkedAppAndWatchTests = function () {
  const appProcess = childProcess.fork('./test/endpoint/startForkedApp');
  new Promise(function (resolve, reject) {
    appProcess.on('message', function (json) {
      if (json.message === 'Application Ready') {
        resolve(json);
      } else {
        reject(json);
      }
    })
  })
    .then(function () {
      let testRunPromise = undefined;
      let process = undefined;
      const testWatcher = webpackRunner.watch(testWebpackConfig, function () {
        if (testRunPromise) {
          testRunPromise = testRunPromise
            .then(function () {
              const forkInfo = forkJasmine();
              process = forkInfo.process;
              return forkInfo.promise;
            }, function (err) {
              console.log('Exiting:', err);
              process.exit(-1);
            })
        } else {
          const forkInfo = forkJasmine();
          testRunPromise = forkInfo.promise;
          process = forkInfo.process;
        }
      });
      appProcess.on('exit', function () {
        testRunPromise.catch(function () {
          return 'All good';
        });
        process.kill('SIGINT');
        console.log('cancelling test watcher');
        testWatcher.close();
      })
    });
  return appProcess;
};


process.on('exit', function () {
  console.log("Caught interrupt signal");
  removeTempDirectory();
  appWatcher.close();
});
const webpackRunner = require('../webpackRunner');
const serverWebpackConfig = require('../../server/webpack.config');
const testWebpackConfig = require('./webpack.config');
const childProcess = require('child_process');
const Promise = require('bluebird');

const runHelpers = require('../run-helpers');
const forkHelpers = require('./../fork-helpers');

const forkOptions = {
  env: {
    PORT: 3001,
    NODE_ENV: 'test'
  }
};

function forkJasmine() {
  return forkHelpers.forkJasmine('test/endpoint', '.tmp', 'test.js', __dirname + '/../../../test-output', 'endpoint.xml',
    forkOptions);
}

const removeTempDirectory = function () {
  runHelpers.removeTempDirectory(__dirname + '/.tmp')
};

let appProcess;

const appWatcher = webpackRunner.watch(serverWebpackConfig, function () {
  if (!appProcess) {
    appProcess = startForkedAppAndWatchTests();
  } else {
    appProcess.kill('SIGINT');

    appProcess = startForkedAppAndWatchTests();
  }
});

let startForkedApp = function () {
  const appProcess = childProcess.fork('./test/endpoint/startForkedApp', forkOptions);
  childProcess.fork('./test/endpoint/startForkedApp');
  const appIsReadyPromise = new Promise(function (resolve, reject) {
    appProcess.on('message', function (json) {
      if (json.message === 'Application Ready') {
        resolve(json);
      } else {
        reject(json);
      }
    })
  });
  return {appProcess, appIsReadyPromise};
};

const startForkedAppAndWatchTests = function () {
  const {appProcess, appIsReadyPromise} = startForkedApp();
  appIsReadyPromise
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
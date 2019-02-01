const [, , specDirectory, tempDirectory, testFilePath, jasmineSavePath, filePrefix] = process.argv;

const runHelpers = require('./run-helpers');
const startJasmine = function () {
  return runHelpers.startJasmine(specDirectory, tempDirectory, testFilePath, jasmineSavePath, filePrefix);
};

startJasmine()
  .then(function () {
    process.exit(0);
  }, function (err) {
    console.log('Exiting fork:', err);
    process.exit(-1);
  });
const ConfigParser = require('protractor/built/configParser').ConfigParser;
const Runner = require('protractor/built/runner').Runner;
const configParser = new ConfigParser();
configParser.addFileConfig(__dirname + '/.tmp/config.js');

const runner = new Runner(configParser.getConfig());
runner.run()
  .then(function (code) {
    process.exit(code);
  }, function (err) {
    console.log('Exiting fork:', err);
    process.exit(-1);
  });
;
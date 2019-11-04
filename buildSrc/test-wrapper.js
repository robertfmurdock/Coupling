const childProcess = require('child_process');
const fs = require('fs');

const testFilePath = process.argv.slice(2);

if (!fs.existsSync(`${testFilePath}`)) {
  console.log(`No test file at '${testFilePath}' - skipping.`);
  return
}

const fork = childProcess.fork(__dirname + '/test-run', testFilePath, {
  // silent: true,
  stdio: 'pipe',
  env: process.env
});

let allLines = "";

fork.on('exit', function (exit) {
  if (exit !== 0) {
    console.log('exit code', exit)
    console.log(allLines)
  }

  process.exit(exit);
});

const readline = require('readline');
const rl = readline.createInterface({
  input: fork.stdout,
  output: process.stdout,
  terminal: false
});

rl.on('line', function (line) {
  allLines += line + "\n";
  try {
    const json = JSON.parse(line);

    if (json.properties.type === 'TestEnd') {
      process.stdout.write('.');

      if (json.properties.status === 'failed') {
        process.stdout.write('F\n');
        console.log(json.properties.failures)
      }
    }
  } catch (e) {
  }
});

const rl2 = readline.createInterface({
  input: fork.stderr,
  output: process.stdout,
  terminal: false
});

rl2.on('line', function (line) {
  console.log('pipe', line);
});
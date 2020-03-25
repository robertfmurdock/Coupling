const config = require('../../webpack.config');
const clone = require('ramda/src/clone');
const path = require('path');
const nodeExternals = require('webpack-node-externals');

const jsPath = path.resolve(__dirname, './');

config.entry = {
  config: path.resolve(jsPath, './protractor-conf.ts'),
  test: path.resolve(jsPath, './test.js')
};

config.module.rules.push({
  test: /\.(css)$/,
  loader: 'css-loader',
  options: {
    modules: {
      context: path.resolve(__dirname, '../../../build/js/packages/Coupling-client/'),
    },
    onlyLocals: true,
  }
});

config.module.rules.push({
  test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
  loader: 'url-loader?limit=100000'
});

config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: '[name].js',
  libraryTarget: 'commonjs'
};

config.target = 'node';
config.externals = [nodeExternals({modulesDir: path.resolve(__dirname, '../../build/js/node_modules')}), nodeExternals()];
config.mode = "development";


function testResolve() {
  let resolve = clone(config.resolve);
  resolve.modules = [
    path.resolve(__dirname, '../../../build/js/node_modules')
  ];

  return resolve;
}

config.resolve = testResolve();


module.exports = config;

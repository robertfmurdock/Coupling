const config = require('../../webpack.config');
const path = require('path');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

config.entry = {
  config: path.resolve(jsPath, './protractor-conf.ts'),
  test: path.resolve(jsPath, './test.js')
};

config.module.rules.push({
  test: /\.(css)$/,
  loader: 'css-loader?' + JSON.stringify({minimize: true}),
  options: {
    modules: {
      context: path.resolve(__dirname, '../../../client/')
    }
  }
});

config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: '[name].js',
  libraryTarget: 'commonjs'
};

config.target = 'node';
config.externals = [nodeExternals()];
config.mode = "development";
module.exports = config;

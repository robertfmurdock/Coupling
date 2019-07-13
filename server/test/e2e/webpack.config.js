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
  loader: 'css-loader',
  options: {
    modules: {
      context: path.resolve(__dirname, '../../../client/'),
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
config.externals = [nodeExternals()];
config.mode = "development";
module.exports = config;

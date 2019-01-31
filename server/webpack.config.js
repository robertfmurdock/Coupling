const webpack = require('webpack');
const path = require('path');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

const config = {
  mode: "production",
  entry: path.resolve(jsPath, './app.ts'),
  output: {
    path: path.resolve(__dirname, '../build'),
    filename: 'app.js',
    libraryTarget: 'commonjs'
  },
  node: {
    __filename: false,
    __dirname: false
  },
  devtool: 'eval',
  target: 'node',
  externals: [nodeExternals()],
  resolve: {
    extensions: ['.js', '.ts'],
    modules: [
      path.resolve(__dirname, '../engine/build/classes/kotlin/js/main'),
      path.resolve(__dirname, '../engine/build/node_modules_imported'),
      path.resolve(__dirname, '../node_modules')
    ]
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        loader: 'ts-loader'
      }
    ]
  },
  plugins: []
};

if (process.env.NODE_ENV === 'production') {
  config.plugins = config.plugins.concat([
    new webpack.optimize.OccurrenceOrderPlugin()
  ]);
}

module.exports = config;
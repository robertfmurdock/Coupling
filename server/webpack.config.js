const webpack = require('webpack');
const path = require('path');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

const config = {
  mode: "production",
  entry: path.resolve(jsPath, './src/main/javascript/app.ts'),
  output: {
    path: path.resolve(__dirname, 'build/executable'),
    filename: 'app.js',
    libraryTarget: 'commonjs'
  },
  node: {
    __filename: false,
    __dirname: false
  },
  devtool: 'eval',
  target: 'node',
  externals: [
    nodeExternals({modulesDir: path.resolve(__dirname, '../build/js/node_modules')}),
    nodeExternals()
  ],
  resolve: {
    extensions: ['.js', '.ts'],
    modules: [
      path.resolve(__dirname, '../build/js/node_modules'),
      path.resolve(__dirname, 'node_modules')
    ]
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        loader: 'ts-loader'
      },
      {
        test: /\.js$/,
        use: ["source-map-loader"],
        enforce: "pre"
      },
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
const webpack = require('webpack');
const path = require('path');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

const config = {
  mode: "production",
  entry: "Coupling-server",
  output: {
    path: path.resolve(__dirname, 'build/executable'),
    filename: 'app.js',
    libraryTarget: 'commonjs'
  },
  node: {
    __filename: false,
    __dirname: false
  },
  devtool: 'source-map',
  target: 'node',
  externals: [
    nodeExternals({
      modulesDir: path.resolve(__dirname, '../build/js/node_modules'),
      allowlist: ['Coupling-server', 'uuid']
    })
  ],
  resolve: {
    extensions: ['.js'],
    modules: [
      process.env.NODE_PATH,
      path.resolve(__dirname, 'build/processedResources/Js/main'),
      path.resolve(__dirname, 'node_modules')
    ]
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        use: ["kotlin-source-map-loader"],
        enforce: "pre"
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
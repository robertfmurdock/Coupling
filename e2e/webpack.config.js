const path = require('path');
const nodeExternals = require('webpack-node-externals');
require('css-loader')

const jsPath = path.resolve(__dirname, './');

let rootProjectNodeModules = path.resolve(__dirname, '../build/js/node_modules');

const config = {
  node: {
    __filename: false,
    __dirname: false
  },
  devtool: 'eval',
  target: 'node',
  module: {
    rules: [
      {
        test: /\.(md|graphql)$/i, use: 'raw-loader'
      },
      {
        test: /\.(css)$/,
        loader: 'css-loader',
        options: {
          esModule: false,
          modules: {
            localIdentContext: path.resolve(__dirname, '../build/js/packages/Coupling-client/'),
            exportOnlyLocals: true,
          },
        }
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader',
        options: {
          limit: 100000
        }
      }
    ]
  },
  plugins: [],
  resolve: {
    extensions: ['.js'],
    modules: [
      rootProjectNodeModules,
    ]
  },
  resolveLoader: {
    modules: [rootProjectNodeModules],
  }

};

config.entry = {
  config: path.resolve(jsPath, './wdio.conf.mjs'),
  test: "Coupling-e2e-e2eTest"
};

config.output = {
  path: path.resolve(__dirname, 'build/.tmp'),
  filename: '[name].js',
  libraryTarget: 'commonjs'
};

config.externals = [
  nodeExternals({
    modulesDir: path.resolve(__dirname, '../build/js/node_modules'),
    allowlist: ["Coupling-e2e-e2eTest"]
  }),
  nodeExternals()
];
config.mode = "development";

module.exports = config;

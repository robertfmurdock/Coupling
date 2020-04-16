const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const webpack = require('webpack');
const path = require('path');
const WebpackCleanupPlugin = require('webpack-cleanup-plugin');
const jsPath = path.resolve(__dirname, './build/processedResources/Js/main');

const config = {
  entry: path.resolve(jsPath, './com/zegreatrob/coupling/client/app.ts'),
  mode: process.env.NODE_ENV,
  output: {
    path: path.resolve(__dirname, 'build/lib/main'),
    filename: 'main.js'
  },
  devtool: 'inline-source-map',
  resolve: {
    modules: [
      path.resolve(__dirname, '../build/js/packages/Coupling-client/kotlin-dce'),
      path.resolve(__dirname, 'node_modules'),
      jsPath
    ],
    extensions: ['.webpack.js', '.web.js', '.ts', '.tsx', '.js', '.json'],
  },
  externals: {
    ws: {},
    gapi: {}
  },
  module: {
    rules: [
      {
        test: /\.ts(x?)$/,
        use: [
          'cache-loader',
          'babel-loader',
          'ts-loader?' + JSON.stringify({
            silent: true
          })
        ]
      },
      {
        test: /\.js$/,
        use: [
          'cache-loader',
          "source-map-loader"
        ],
        enforce: "pre"
      },
      {
        test: /\.md$/i,
        use: 'raw-loader',
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          'cache-loader',
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              hmr: process.env.NODE_ENV === 'development',
            },
          },
          {
            loader: 'css-loader',
            options: {
              modules: 'global'
            }
          },
          'sass-loader',
        ],
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: [
          'cache-loader',
          'url-loader?limit=100000'
        ]
      }
    ]
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: './styles.css'
    }),
    new WebpackCleanupPlugin({
      quiet: true
    }),
  ],
  performance: {
    hints: isProductionMode() ? 'error' : false,
    maxAssetSize: 1000000,
    maxEntrypointSize: 1000000
  }
};

function isProductionMode() {
  return process.env.NODE_ENV === 'production';
}

if (isProductionMode()) {
  config.devtool = 'cheap-module-source-map';

  config.plugins = config.plugins.concat([
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.OccurrenceOrderPlugin()
  ]);
}

module.exports = config;
const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const WebpackCleanupPlugin = require('webpack-cleanup-plugin');
const jsPath = path.resolve(__dirname, './app');

const config = {
  entry: path.resolve(jsPath, './app.ts'),
  mode: "production",
  output: {
    path: path.resolve(__dirname, '../public/app/build'),
    filename: 'main.js'
  },
  devtool: 'source-map',
  resolve: {
    extensions: ['.webpack.js', '.web.js', '.ts', '.js', '.json']
  },
  externals: {
    ws: {}
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        loader: 'ts-loader?' + JSON.stringify({
          silent: true
        })
      },
      {
        test: /\.(pug)$/,
        loader: 'pug-loader',
        include: jsPath
      },
      {
        test: /\.(css)$/,
        loader: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: 'css-loader?' + JSON.stringify({minimize: true})
        })
      },
      {
        test: /\.(scss)$/,
        loader: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: 'css-loader?' + JSON.stringify({minimize: true}) + '!sass-loader'
        })
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=100000'
      }
    ]
  },
  plugins: [
    new WebpackCleanupPlugin({
      quiet: true,
      exclude: ["vendor/*"],
    }),
    new ExtractTextPlugin({filename: './styles.css', allChunks: true}),
    new webpack.DllReferencePlugin({
      context: '.',
      manifest: require('../public/app/build/vendor/vendor-manifest.json')
    }),
    new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en/)
  ],
  performance: {
    hints: 'error'
  }
};

if (process.env.NODE_ENV === 'production') {
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
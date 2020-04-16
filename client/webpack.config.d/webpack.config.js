const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

let resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/Js/main');

config.externals = {jquery: "jQuery"};
config.resolve.modules.push(
  resourcesPath
);
config.resolve.extensions = ['.js', '.ts'];

config.module.rules.push({
    test: /\.ts(x?)$/,
    use: [
      'cache-loader',
      'babel-loader',
      'ts-loader?' + JSON.stringify({silent: true})
    ]
  },
  {test: /\.md$/i, use: 'raw-loader'}, {
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
  }, {
    test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
    loader: [
      'cache-loader',
      'url-loader?limit=100000'
    ]
  }
);
config.plugins.push(new MiniCssExtractPlugin({
  filename: './styles.css'
}));

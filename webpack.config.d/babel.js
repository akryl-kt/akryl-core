config.module.rules.push({
  test: /\.m?js$/,
  exclude: /(node_modules|bower_components|packages_imported)/,
  use: {
    loader: 'babel-loader',
    options: {
      presets: ['@babel/preset-env'],
      plugins: [
        ['babel-plugin-akryl', {}]
      ],
    }
  }
});

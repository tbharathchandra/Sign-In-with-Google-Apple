'use strict';

module.exports = function(app) {
    var user = require("../controllers/userController");

    app
    .route("/")
    .get(user.ping);

    app
    .route("/loginWithGoogle")
    .post(user.loginWithGoogle);

    app
    .route("/loginWithSessionToken")
    .post(user.loginWithSessionToken);

    app
    .route("/logout")
    .post(user.logout);

    app
    .route("/disconnect")
    .post(user.disconnect);
}
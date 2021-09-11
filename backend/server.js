'use strict'

// require express and bodyParser
require('dotenv').config()
const  express = require("express");
const  bodyParser = require("body-parser");
require("./config/db");

var routes = require("./api/routes/userRoutes");

// create express app
const  app = express();

// define port to run express app
const  port = process.env.PORT || 3000;

// use bodyParser middleware on express app
app.use(bodyParser.urlencoded({ extended:true }));
app.use(bodyParser.json());

// Add endpoint
routes(app);

// Listen to server
app.listen(port, () => {
console.log(`Server running at http://localhost:${port}`);
});
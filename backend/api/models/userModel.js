'use strict';
// Import mongoose
    const mongoose = require("mongoose");

// Declare schema and assign Schema class
    const Schema = mongoose.Schema;

// Create Schema Instance and add schema propertise
    const UserSchema = new Schema({
        id: {
            type:String,
            required:true,
            unique:true
        },
        provider: {
            type:String,
            required:true,
        },
        providerId: {
            type:String,
            required:true,
            unique:true
        },
        name:{
            type:String,
            required:true
        },
        email:{
            type:String,
            required:true,
        },
        accessToken:{
            type:String,
            required:false
        },
        refreshToken:{
            type:String,
            required:false
        },
        sessionToken:{
            type:String,
            required:false
        },
        createdOn: {
            type:Date,
            default:Date.now
        }
    });

// create and export model
module.exports = mongoose.model("userModel", UserSchema);
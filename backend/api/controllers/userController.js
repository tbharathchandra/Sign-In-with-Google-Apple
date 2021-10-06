const User = require("../models/userModel");
const {OAuth2Client} = require('google-auth-library');
const uuid = require('uuid');
const axios = require('axios');
const jwksClient = require('jwks-rsa');
const jwt_decode = require('jwt-decode');
const jwt = require('jsonwebtoken');

const ANDROID_CLIENT_ID = process.env.ANDROID_CLIENT_ID; // android client
const WEB_CLIENT_ID = process.env.WEB_CLIENT_ID; // web client
const WEB_CLIENT_SECRET = process.env.WEB_CLIENT_SECRET;
const ANDROID = "android";
const APPLE = "apple";

exports.loginWithGoogle = async (req, res) => {
    const androidClient = new OAuth2Client(ANDROID_CLIENT_ID);
    let idToken = req.body.idToken;
    let authCode = req.body.authCode;
    let providerId = req.body.providerId;
    let provider = req.body.provider;
    if(provider===ANDROID){
        try{
            const ticket = await androidClient.verifyIdToken({
                idToken: idToken,
                audience: WEB_CLIENT_ID,  // Specify the CLIENT_ID of the app that accesses the backend web client
            });
            const payload = ticket.getPayload();
            const userid = payload['sub'];
            console.log("payload: ",payload);

            let id = uuid.v1();
            let sessionToken = uuid.v1();

            // Check if the user is an existing one
            let existinguser = await User.findOne({providerId:providerId}).exec();

            const webClient = new OAuth2Client(WEB_CLIENT_ID, WEB_CLIENT_SECRET, "");

            const tokens = await webClient.getToken(authCode);

            console.log(tokens.tokens);

            if(existinguser!==null){
                await User.findOneAndUpdate({providerId:providerId}, {accessToken:tokens.tokens.access_token,refreshToken:tokens.tokens.refresh_token,sessionToken:sessionToken}).exec();
                return res.status(200).json({
                    name:payload.name,
                    email:payload.email,
                    sessionId:sessionToken, 
                    id:existinguser.id,
                });
            }

            let user = new User({
                id: id,
                providerId: userid,
                provider: provider,
                name:payload.name,
                email:payload.email,
                accessToken:tokens.tokens.access_token,
                refreshToken:tokens.tokens.refresh_token,
                sessionToken:sessionToken,
            });
            
            await user.save()

            console.log("success user creation with google our session token:",sessionToken);
            
            res.status(200).json({
                name:payload.name,
                email:payload.email,
                sessionId:sessionToken,
                id:id,
            });

        } catch(err) {
            console.log(err);
            res.status(401).json({msg:"Failed authenticatiing user", err:err})
        }
    }else if(provider===APPLE){

    }
    
}


exports.loginWithSessionToken = async (req, res) => {
    let id = req.body.id;
    let sessionId = req.body.sessionId;
    let user = null;
    try {
        user = await User.findOne({id:id}).exec();
        console.log("user -- ", user)
        if(user.sessionToken===sessionId) {
            try{
                const response = await axios.post(
                    REFRESH_URL(WEB_CLIENT_ID, WEB_CLIENT_SECRET, user.refreshToken),
                );
                console.log("Access Token -- ", response.data.access_token)
                if(response.data.access_token) {
                    await User.findOneAndUpdate({id:id}, {accessToken:response.data.access_token}).exec();
                    return res.status(200).json({msg:"User can log in"});
                }
            }
            catch(err) {
                await User.findOneAndUpdate({id:id}, {sessionToken:"", accessToken:"", refreshToken:""}).exec();
                res.status(404).json({msg:"Unable to refresh access token"});
            }
            // return res.status(200).json({msg:"User can log in"});
        }
        else res.status(404).json({msg:"Session Id not found"});
    }catch(err) {
        res.status(404).json({msg:"Failed login", err:err});
    }
}

exports.logout = async (req, res) => {
    let id = req.body.id;
    try{
        let user = await User.findOne({id:id}).exec();
        if(user) {
            if(user.refreshToken)
                await axios.get(REVOKE_TOKEN(user.refreshToken));
            await User.findOneAndUpdate({id:id}, {sessionToken:"", accessToken:"", refreshToken:""}).exec();
            return res.status(200).json({msg:"User logged out"});
        }
    }catch(err) {
        console.log(err);
        res.status(401).json({msg:"Failed logging user out", err:err});
    }
}

exports.ping = (req, res) => {
    res.status(200).send("Hello World!");
}

exports.disconnect = async (req, res) => {
    let id = req.body.id;
    try {
        let user = await User.findOne({id:id}).exec();
        if(user) {
            await User.deleteOne({id:id}).exec();
            return res.status(200).json({msg:"User logged out"});
        }
    }catch(err) {
        console.log(err);
        res.status(401);
    }
}

exports.securityEventReceiver = (req, res) => {
    let issuer = "accounts.google.com";
    let jwksUri = "https://www.googleapis.com/oauth2/v3/certs";
    // try{
    //     let unverifiedJWT = jwt_decode(req.token, { header: true });
    //     let kid = unverifiedJWT.kid;

    //     const jwksclient = jwksClient({
    //         jwksUri: jwksUri,
    //       });
    //     const key = await jwksclient.getSigningKeys(kid);
    //     const signingKey = key.getPublicKey();
        
    //     jwt.verify(req.token, signingKey, { algorithms: ['RS256'], issuer:issuer }, function (err, payload){
    //         if(err){
    //             console.log(err);
    //             return res.status(401);
    //         }
    //         console.log("Security payload -----> "+payload);
    //     });

    // }catch(err) {
    //     console.log(err);
    //     res.status(401);
    // }
    console.log('Google Security token'+req.token);
}

const REFRESH_URL = (client_id, client_secret, refresh_token) =>
  `https://www.googleapis.com/oauth2/v4/token?client_id=${client_id}&client_secret=${client_secret}&refresh_token=${refresh_token}&grant_type=refresh_token`;

const REVOKE_TOKEN = (refresh_token) =>
    `https://accounts.google.com/o/oauth2/revoke?token=${refresh_token}`;


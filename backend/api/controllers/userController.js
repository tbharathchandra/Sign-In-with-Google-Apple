const User = require("../models/userModel");
const {OAuth2Client} = require('google-auth-library');
const uuid = require('uuid');
const ANDROID_CLIENT_ID = process.env.ANDROID_CLIENT_ID; // android client
const WEB_CLIENT_ID = process.env.WEB_CLIENT_ID; // web client
const WEB_CLIENT_SECRET = process.env.WEB_CLIENT_SECRET;
const ANDROID = "android";

exports.loginWithGoogle = async (req, res) => {
    const androidClient = new OAuth2Client(ANDROID_CLIENT_ID);
    let idToken = req.body.idToken;
    let authCode = req.body.authCode;
    let providerId = req.body.providerId;
    let provider = req.body.provider;
    if(provider==ANDROID){
        try{
            const ticket = await androidClient.verifyIdToken({
                idToken: idToken,
                audience: WEB_CLIENT_ID,  // Specify the CLIENT_ID of the app that accesses the backend web client
            });
            const payload = ticket.getPayload();
            const userid = payload['sub'];
            console.log("payload: ",payload);

            let id = (new Date()).getTime().toString(36);
            let sessionToken = uuid.v1();

            // Check if the user is an existing one
            let existinguser = await User.findOne({providerId:providerId}).exec();

            if(existinguser!==null) {
                await User.findOneAndUpdate({providerId:providerId}, {sessionToken:sessionToken}).exec();
                return res.status(200).json({
                    name:payload.name,
                    email:payload.email,
                    sessionId:sessionToken,
                    id:existinguser.id,
                });
            }

            const webClient = new OAuth2Client(WEB_CLIENT_ID, WEB_CLIENT_SECRET, "");

            const tokens = await webClient.getToken(authCode);

            console.log(tokens.tokens);

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
    }
    
}


exports.loginWithSessionToken = async (req, res) => {
    let id = req.body.id;
    let sessionId = req.body.sessionId;
    try {
        let user = await User.findOne({id:id}).exec();
        if(user.sessionToken===sessionId) return res.status(200).json({msg:"User can log in"});
        else res.status(404).json({msg:"Session Id not found"});
    }catch(err) {
        console.log(err);
        res.status(404).json({msg:"Failed login", err:err});
    }
}

exports.logout = async (req, res) => {
    let id = req.body.id;
    try{
        let user = await User.findOne({id:id}).exec();
        if(user) {
            await User.findOneAndUpdate({id:id}, {sessionToken:""}).exec();
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
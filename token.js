const simpleOAuth2 = require('simple-oauth2');

const oauth2 = simpleOAuth2.create({
  client: {
    id: 'tu_client_id',
    secret: 'tu_client_secret'
  },
  auth: {
    tokenHost: 'https://login.microsoftonline.com',
    authorizePath: '/common/oauth2/v2.0/authorize',
    tokenPath: '/common/oauth2/v2.0/token'
  }
});

const authorizationUri = oauth2.authorizationCode.authorizeURL({
  redirect_uri: 'http://localhost:3000/callback', // Cambia esta URL a la que corresponda
  scope: 'openid profile offline_access https://outlook.office365.com/mail.read', // Cambia los ámbitos según tus necesidades
  state: 'una_cadena_aleatoria_generada'
});

console.log('Autoriza esta aplicación visitando esta URL:', authorizationUri);

const tokenConfig = {
  code: 'el_codigo_de_autorizacion_recibido', // Cambia esto al código de autorización que recibas
  redirect_uri: 'http://localhost:3000/callback' // Cambia esto a la URL correcta
};

async function getToken() {
  try {
    const result = await oauth2.authorizationCode.getToken(tokenConfig);
    const accessToken = oauth2.accessToken.create(result);
    console.log('Token de acceso:', accessToken.token.access_token);
  } catch (error) {
    console.error('Error al obtener el token:', error.message);
  }
}

// Llamada a la función para obtener el token
getToken();

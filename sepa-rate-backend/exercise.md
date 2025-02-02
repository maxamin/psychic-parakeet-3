#Setting up keycloak and logging in
## Keycloak
- Read the [Getting started](https://www.keycloak.org/docs/latest/getting_started/index.html) to download and start the keycloak server for your operating system.
- The first problem we encounter is that Keycloak runs on the same port as our backend. So we'll have to [change that](https://www.keycloak.org/docs/2.5/server_installation/topics/network/ports.html).
- Now you should be able to open the Keycloak UI and create the admin's account. Be sure to remember these credentials.
- Create a client in the Keycloak UI. This client is used to connect to Keycloak via REST
- Copy some or all of the users for sepa-rate's backend to keycloak. 
    - Every user also has a 'parameters' form. This is where you can put the country the user belongs to.

** Keycloak uses a H2 db by default (it supports most well known relational db brands). This db will suffice for now.

## Backend
- Add Keycloak dependency: _compile('org.keycloak:keycloak-spring-boot-2-starter:4.0.0.Final')_
- Add a Spring security config that integrates with Keycloak. A good start can be found (here)[https://www.baeldung.com/spring-boot-keycloak]
    - To connect with the UI, the configure method will need to changed slightly to:
        -	@Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.cors()
                    .and().authorizeRequests()
                    .and().csrf().disable();
        }
     - We also need to add a corsConfiguration. eg:
        -     @Bean
              CorsConfigurationSource corsConfigurationSource() {
                  final CorsConfiguration configuration = new CorsConfiguration();
                  configuration.setAllowedOrigins(Collections.singletonList("*"));
                  configuration.setAllowedMethods(asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
                  configuration.setAllowCredentials(true);
                  configuration.setAllowedHeaders(asList("Authorization", "Cache-Control", "Content-Type"));
                  final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                  source.registerCorsConfiguration("/**", configuration);
                  return source;
              }
- Add some keycloak related properties in the yaml file:
    - keycloak:
        realm: "master"
        auth-server-url: http://localhost:yourport/auth
        ssl-required: external
        resource: SpringBootClient
        public-client: true
        confidential-port: 0	
        
- Remove the userRepository, User and UserDto. These will no longer be used
- Instead of fetching the users via a db call, we'll have to perform a rest call to Keycloak to log in.
    - This is a POST to <keycloak-url>/auth/realms/master/protocol/openid-connect/token
    - Add a header: 'Content-Type' : 'application/x-www-form-urlencoded'
    - The body of the request is a map containing the following parameters:
        - grant_type=password
        -  client_id=Application's client id
        -  username=the username
        -  password=the password
    - The REST call returns a 'AccessTokenResponse' object. This object is to be returned to the UI.

** It's also possible to call keycloak directly from the UI to perform a login. Use whichever method you are more familiar with.

## UI
- implement rest call to backend or keycloak to login.
    - This will return an encoded token, which can be decoded using the 'jwt_decode' (npm install --save jwt-decode). After decoding it, store it somewhere (eg in a service or in localstorage)
        - This token will contain the user's username and an access_token. The country for the user's account is fetched by another call. For now, hardcode it to 'Belgium' (we will fix this later).
- Search for 'cookieService' and replace all of this code with references to the saved token. Also clear your browser's cookies to be thorough
- When logging out (found in app.component), clear the stored token
- The application should now work  the same as before (except for the fact that all users have 'Belgium' as their country).
- For extra marks, replace the 'is user logged in'-checks in the oninit parts of the components by an Angular guard


#Fetching the user's country
## Backend
- Implement [the following GET call]( https://www.keycloak.org/docs-api/2.5/rest-api/index.html#_get_users_2)
    - tip: use queryParam 'username'
    - This will return a list of UserRepresentation objects that contains a map of attributes. Country will be among them.
        - However, this REST call can not be done without the proper authorisation. We will need an access token for the admin account first
        - Perform the login call using the admin's credentials, then create an Authorization header with the value 'Bearer ' + the token present in the AccessTokenResponse
        - Add this header to the call to get the UserRepresentations
## UI
- Perform the REST call and populate the country field correctly

#Securing the REST api
## Backend
- Secure the REST API using the methods seen in the previous labs. There are no roles (yet), so users simply have to be authenticated
    - Make sure to keep the 'login' REST accessible for everyone

#UI
- Implement an interceptor (or add it to each request done by the services) that attaches a 'Authorization' header to each outgoing rest-call to the backend end. 
    - This header has a value of the word 'Bearer' followed by a space and then the access_token value.


#Adding roles

Experiment a bit with role-based access. The token received in the first story also contains the user's roles. 
EG: make the separations overview page available to everyone, make the separation detail page available to users with role 'DETAIL' and the ratings overview page only for people with the 'RATING' role
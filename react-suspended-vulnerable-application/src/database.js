export const database = {
    authorName: "Kate Libby",
    aboutAuthor: "Penetration Tester at Ellingson Mineral Company. Experience developing and delivering custom gibson hacking payloads via the well known metasploit framework.",
    twitterLink: "https://twitter.com/katelibyy",
    testimonial: {
        cite: "zero cool",
        text: "mess with the best, die like the rest"
    },
    // packageManifest: '<img src=x onError=alert(1) />',
    packageManifest: {
        "name": "<img src=x onError=alert(1)>",
        "version": "0.0.0-development",
        "description": "cruise the gibson to find the garbage file",
        "bin": {
          "gibson": "<img src=x onError=alert(1)>",
        },
        "scripts": {
          "lint": "standard && eslint . --ignore-path .gitignore && yarn run lint:lockfile",
          "lint:lockfile": "lockfile-lint --path yarn.lock --type yarn --validate-https --allowed-hosts npm yarn",
          "test": "jest",
          "start": "node hack-the-planet.js"
        },
        "author": {
          "name": "Kate Libby",
          "email": "katelibby@ellingson.com"
        },
        "license": "Apache-2.0",
        "repository": {
          "type": "git",
          "url": "git+https://github.com/katelibby/gibson-explorer.git"
        }
    },
    authorScreenshotURL: 'https://miro.medium.com/max/1838/0*bljFeVNLqEFudixU.png',
    authorScreenshotDescription: "loremipsum"
    // authorScreenshotDescription: 'a onLoad=alert(1)',
};

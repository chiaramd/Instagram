# Project 3 - Parstagram

Parstagram is a photo sharing app using Parse as its backend.

Time spent: 20 hours spent in total

## User Stories

The following **required** functionality is completed:

- [X] User sees app icon in home screen.
- [X] User can sign up to create a new account using Parse authentication
- [X] User can log in and log out of his or her account
- [X] The current signed in user is persisted across app restarts
- [X] User can take a photo, add a caption, and post it to "Instagram"
- [X] User can view the last 20 posts submitted to "Instagram"
- [X] User can pull to refresh the last 20 posts submitted to "Instagram"
- [X] User can tap a post to view post details, including timestamp and caption.

The following **stretch** features are implemented:

- [ ] Style the login page to look like the real Instagram login page.
- [X] Style the feed to look like the real Instagram feed.
- [X] User should switch between different tabs - viewing all posts (feed view), capture (camera and photo gallery view) and profile tabs (posts made) using a Bottom Navigation View.
- [X] User can load more posts once he or she reaches the bottom of the feed using infinite scrolling.
- [X] Show the username and creation time for each post
- [X] After the user submits a new post, show an indeterminate progress bar while the post is being uploaded to Parse
- User Profiles:
  - [X] Allow the logged in user to add a profile photo
  - [X] Display the profile photo with each post
  - [ ] Tapping on a post's username or profile photo goes to that user's profile page
- [X] User can comment on a post and see all comments for each post in the post details screen.
- [X] User can like a post and see number of likes for each post in the post details screen.
- [X] Create a custom Camera View on your phone.
- [X] Run your app on your phone and use the camera to take the photo

The following **additional** features are implemented:

- Capture fragment
  - [X] Camera is embedded in the app
  - [X] User can select a photo from the gallery
  - [X] Thumbnail in the capture view displays the last gallery photo and selecting it takes the user to the gallery
  - [X] Tapping on the loaded preview image allows the user to re-take/select a photo
- Profile page
  - [X] User's posts are displayed in a gridview and selecting a photo loads the detail page for that post


Please list two areas of the assignment you'd like to **discuss further with your peers** during the next class (examples include better ways to implement something, how to extend your app in certain ways, etc):

1. UI
2. Parse modelling

## Video Walkthrough

Here's a walkthrough of implemented user stories:

<img src='Parstagram_GIF' title='Video Walkthrough' width='' alt='Video Walkthrough' />

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Credits

List an 3rd party libraries, icons, graphics, or other assets you used in your app.

- [Android Async Http Client](http://loopj.com/android-async-http/) - networking library
- Butterknife


## Notes


## License

    Copyright 2019 Chiara Darnton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

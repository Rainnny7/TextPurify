# API
The backend API for TextPurify, responsible for the filtering and moderation of text content. The API is built using Springboot, a modern Java web framework. The API can be accessed [here](https://purify.rainnny.club).

## Endpoints

### POST `/content/process`
This endpoint is used to filter text content. The request body should be an encoded form with the following fields:

| Field         | Description                                                               | Required |
|---------------|---------------------------------------------------------------------------|----------|
| `content`     | The text content to be filtered.                                          | Yes      |
| `replaceChar` | The character to use for filtered content replacement.                    | No       |
| `ignoredTags` | The tags to ignore during filtering (E.g: `VULGARITY`, or `ADVERTISING`). | No       |
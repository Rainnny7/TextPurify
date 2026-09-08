# API
The backend API for TextPurify, responsible for the filtering and moderation of text content. The API is built using Springboot, a modern Java web framework. The API can be accessed [here](https://purify.rainnny.club).

## Endpoints

### POST `/content/process`
This endpoint is used to filter text content. The request body should be an encoded form with the following fields:

| Field         | Description                                                               | Required |
|---------------|---------------------------------------------------------------------------|----------|
| `content`     | The text content to be filtered.                                          | Yes      |
| `replaceChar` | The character to use for filtered content replacement.                    | No       |
| `ignoredTags` | The tags to ignore during filtering (see below).                            | No       |

#### Content tags
| Tag            | Description                                      |
|----------------|--------------------------------------------------|
| `SEXUAL`       | Explicit sexual content                          |
| `VULGAR`       | Compound vulgar terms                            |
| `HATE_SPEECH`  | Slurs, hate groups, and discriminatory language  |
| `SELF_HARM`    | Self-harm encouragement                        |
| `SHOCK`        | Shock/gore meme references                       |
| `ADVERTISEMENT`| URLs and IP addresses                            |

#### Example

```bash
curl -X POST "https://purify.rainnny.club/content/process" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "content=Check out http://spam.com for deals"
```

Response:

```json
{
  "containsProfanity": true,
  "content": "Check out http://spam.com for deals",
  "replacement": "Check out *************** for deals",
  "matched": ["http://spam.com"],
  "tags": ["ADVERTISEMENT"],
  "score": 0.429
}
```

| Field               | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| `containsProfanity` | Whether any filtered content was detected.                                  |
| `content`           | The original input text.                                                      |
| `replacement`       | The filtered text, with matches replaced by `replaceChar` (default: `*`).   |
| `matched`           | The substrings that were filtered.                                          |
| `tags`              | The content tags that matched.                                              |
| `score`             | A value from 0–1 representing how likely the content is profane.            |

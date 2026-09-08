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

## Admin API

Admin routes let you manage the profanity filter list without editing MongoDB directly. All `/admin/**` endpoints require the `X-Admin-Api-Key` header.

### Configuration

Set the admin API key in `application.yml`:

```yaml
admin:
  api-key: "your-secret-key" # Leave empty to disable admin routes
```

If the key is empty, admin routes return `503 Service Unavailable`. If the key is missing or incorrect, they return `401 Unauthorized`.

### Authentication

Include the admin API key on every admin request:

```bash
-H "X-Admin-Api-Key: your-secret-key"
```

### Admin Endpoints

| Method   | Route                      | Description                                              |
|----------|----------------------------|----------------------------------------------------------|
| `GET`    | `/admin/list`              | Get the full profanity list                              |
| `PUT`    | `/admin/list`              | Replace the entire profanity list                        |
| `POST`   | `/admin/list/reload`       | Reload the list from MongoDB into memory                 |
| `POST`   | `/admin/list/reseed`       | Re-download default lists from GitHub and overwrite DB   |
| `GET`    | `/admin/words`             | Get words/phrases for a tag and language                 |
| `POST`   | `/admin/words`             | Add one or more words or phrases                         |
| `DELETE` | `/admin/words`             | Remove a specific word or phrase                         |
| `GET`    | `/admin/whitelisted-links` | List whitelisted links                                   |
| `POST`   | `/admin/whitelisted-links` | Add a whitelisted link                                 |
| `DELETE` | `/admin/whitelisted-links` | Remove a whitelisted link                              |
| `GET`    | `/admin/stats`             | Get per-tag word and phrase counts                       |

#### Add a word

```bash
curl -X POST "http://localhost:7500/admin/words" \
  -H "X-Admin-Api-Key: your-secret-key" \
  -H "Content-Type: application/json" \
  -d '{"tag":"VULGAR","language":"ENGLISH","terms":["badword"]}'
```

Terms containing spaces are automatically stored as phrases unless `type` is set to `WORD` or `PHRASE`.

#### Remove a word

```bash
curl -X DELETE "http://localhost:7500/admin/words" \
  -H "X-Admin-Api-Key: your-secret-key" \
  -H "Content-Type: application/json" \
  -d '{"tag":"VULGAR","language":"ENGLISH","term":"badword"}'
```

#### Get list stats

```bash
curl "http://localhost:7500/admin/stats" \
  -H "X-Admin-Api-Key: your-secret-key"
```

#### Get full list

```bash
curl "http://localhost:7500/admin/list" \
  -H "X-Admin-Api-Key: your-secret-key"
```

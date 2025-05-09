export class FetchError extends Error {
  response: Response;
  body: unknown;

  constructor(response: Response, body: unknown = 'error.api-fetch') {
    super(typeof body === 'string' ? body : 'error.api-fetch');
    Object.setPrototypeOf(this, FetchError.prototype);
    this.response = response;
    this.body = body;
  }
}

const UNAUTHORIZED_MESSAGE =
  'Ei riittäviä käyttöoikeuksia.\n\n Otillräckliga användarrättigheter. \n\n No access rights.';

export class PermissionError extends Error {
  constructor(message: string = UNAUTHORIZED_MESSAGE) {
    super(message);
  }
}

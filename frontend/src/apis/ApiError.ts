class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public rawBody?: unknown,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export default ApiError;

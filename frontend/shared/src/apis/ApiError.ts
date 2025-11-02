class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public rawBody?: Record<string, string>,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export default ApiError;

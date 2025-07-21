class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public rawBody?: any,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export default ApiError;

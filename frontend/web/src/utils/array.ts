export const chunk = <T>(array: T[], size: number) => {
  const splittedChunkLength = Math.ceil(array.length / size);

  return Array.from({ length: splittedChunkLength }, (_, index) =>
    array.slice(index * size, (index + 1) * size),
  );
};

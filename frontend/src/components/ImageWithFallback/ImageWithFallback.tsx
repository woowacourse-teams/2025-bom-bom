import { ComponentProps } from 'react';
import defaultImage from '#/assets/avif/logo.avif';

interface ImageWithFallbackProps extends ComponentProps<'img'> {
  src: string;
  alt: string;
}

function ImageWithFallback({ src, alt, ...props }: ImageWithFallbackProps) {
  const handleImageError = (event: React.SyntheticEvent<HTMLImageElement>) => {
    const image = event.currentTarget;
    image.src = defaultImage;
  };

  return (
    <img
      src={src.length > 0 ? src : defaultImage}
      alt={alt}
      onError={handleImageError}
      {...props}
    />
  );
}

export default ImageWithFallback;

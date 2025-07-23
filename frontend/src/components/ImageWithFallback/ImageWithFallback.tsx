import { ComponentProps } from 'react';
import defaultImage from '#/assets/bombom.png';

interface ImageWithFallbackProps extends ComponentProps<'img'> {
  src: string;
  alt: string;
}

function ImageWithFallback({ src, alt, ...props }: ImageWithFallbackProps) {
  const handleImageError = (event: React.SyntheticEvent<HTMLImageElement>) => {
    const image = event.currentTarget;
    image.src = defaultImage;
    image.alt = '기본 이미지';
  };

  return (
    <img
      src={src.length > 0 ? src : defaultImage}
      alt={src.length > 0 ? alt : '기본 이미지'}
      onError={handleImageError}
      {...props}
    />
  );
}

export default ImageWithFallback;

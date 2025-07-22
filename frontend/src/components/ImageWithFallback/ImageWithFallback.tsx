import { ComponentProps, useEffect, useState } from 'react';
import defaultImage from '#/assets/bombom.png';

interface ImageWithFallbackProps extends ComponentProps<'img'> {
  src: string;
  alt: string;
}

function ImageWithFallback({ src, alt, ...props }: ImageWithFallbackProps) {
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    if (src.length === 0) {
      setImageError(true);
    }
  }, [src]);

  return (
    <img
      src={imageError ? defaultImage : src}
      alt={imageError ? '기본 이미지' : alt}
      onError={() => setImageError(true)}
      {...props}
    />
  );
}

export default ImageWithFallback;

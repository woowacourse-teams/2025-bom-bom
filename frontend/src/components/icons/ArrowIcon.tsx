import { SVGProps } from 'react';

export default function ArrowIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      width="15"
      height="15"
      viewBox="0 0 15 15"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <g clipPath="url(#clip0_619_985)">
        <path
          d="M3.66675 7.5H11.8334"
          stroke="white"
          strokeWidth="1.16667"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M7.75 3.4165L11.8333 7.49984L7.75 11.5832"
          stroke="white"
          strokeWidth="1.16667"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </g>
      <defs>
        <clipPath id="clip0_619_985">
          <rect
            width="14"
            height="14"
            fill="white"
            transform="translate(0.75 0.5)"
          />
        </clipPath>
      </defs>
    </svg>
  );
}

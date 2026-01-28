FROM public.ecr.aws/lambda/nodejs:20

COPY package*.json ./
RUN npm ci --omit=dev

COPY index.js ./

CMD [ "index.handler" ]

FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./

RUN npm install --registry=https://registry.npmmirror.com

COPY . .

RUN npm run build

FROM nginx:1.27-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN apk add --no-cache gettext

COPY docker-entrypoint.sh /docker-entrypoint.d/99-envsubst-admin.sh
RUN chmod +x /docker-entrypoint.d/99-envsubst-admin.sh

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

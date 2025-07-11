FROM node:23-alpine as build
WORKDIR /app

# Copy package files first for better caching
COPY package*.json ./
RUN npm ci --ignore-scripts

# Copy source code and build
COPY . .
RUN npm run build

FROM nginx:stable-alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
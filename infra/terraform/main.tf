provider "aws" {
  region = "us-east-1"
}

resource "aws_instance" "app_instance" {
  ami           = "ami-0c02fb55956c7d316" # Amazon Linux 2
  instance_type = "t2.micro"

  tags = {
    Name = "docker-app-instance"
  }
}

output "public_ip" {
  value = aws_instance.app_instance.public_ip
}

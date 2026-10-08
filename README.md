# Implementação do Protocolo Go-Back-N via UDP em Java

Trabalho Final da disciplina **Redes de Computadores** – Universidade Federal de Alfenas (UNIFAL-MG).

## Descrição

Este projeto implementa o protocolo **Go-Back-N (GBN)** utilizando **sockets UDP** em Java.

Como o protocolo UDP não oferece confiabilidade, ordenação ou retransmissão de pacotes, toda a lógica necessária foi implementada pela aplicação, seguindo o funcionamento apresentado no livro **Computer Networking: A Top-Down Approach (Kurose & Ross)**.

O sistema é composto por dois módulos:

- **Emissor**
- **Receptor**

que se comunicam através de datagramas UDP para realizar a transferência confiável de arquivos.

---

# Funcionalidades

- Comunicação via UDP.
- Handshake para inicialização da sessão.
- Transferência de arquivos binários.
- Fragmentação em pacotes de até 1024 bytes.
- Janela deslizante Go-Back-N.
- ACK cumulativo.
- Timeout para detecção de perda.
- Retransmissão da janela.
- Descarte de pacotes fora de ordem.
- Simulação de perda de pacotes.
- Encerramento da sessão através de pacote FIN.
- Estatísticas do emissor.
- Estatísticas do receptor.
- Verificação de integridade utilizando MD5.

---

# Estrutura do Projeto

```
go-back-n-udp/
│
├── README.md
│
├── src/
│   │
│   ├── Emissor.java
│   ├── Receptor.java
│   │
│   ├── protocol/
│   │   ├── Packet.java
│   │   ├── PacketType.java
│   │   └── ProtocolConfig.java
│   │
│   ├── sender/
│   │   ├── GBNSender.java
│   │   ├── AckReceiver.java
│   │   └── SenderStats.java
│   │
│   ├── receiver/
│   │   ├── GBNReceiver.java
│   │   ├── LossSimulator.java
│   │   └── ReceiverStats.java
│   │
│   └── util/
│       ├── ArgumentParser.java
│       ├── FileUtils.java
│       └── HashUtils.java
│
├── testes/
│   ├── entrada/
│   └── saida/
│
└── bin/
```

---

# Formato dos Pacotes

Cada datagrama possui o seguinte cabeçalho:

| Campo | Tamanho |
|--------|---------|
| Tipo | 1 byte |
| Número de sequência | 4 bytes |
| Número de ACK | 4 bytes |
| Tamanho dos dados | 2 bytes |
| Payload | até 1024 bytes |

Tipos de pacote:

| Tipo | Valor |
|------|-------|
| DATA | 0 |
| ACK | 1 |
| HANDSHAKE | 2 |
| FIN | 3 |

---

# Funcionamento

## Handshake

O emissor inicia enviando um pacote de controle contendo:

- probabilidade de perda;
- caminho do arquivo de destino;
- tamanho do arquivo;
- hash MD5 do arquivo original.

O receptor responde com um ACK confirmando o início da sessão.

---

## Transferência

O arquivo é dividido em blocos de até **1024 bytes**.

Cada bloco recebe um número de sequência.

O emissor mantém uma janela de transmissão de tamanho **N**.

Enquanto houver espaço na janela:

- envia novos pacotes;
- aguarda ACKs cumulativos;
- desliza a janela conforme os ACKs chegam.

---

## Timeout

Caso um ACK não seja recebido dentro do tempo configurado:

- ocorre timeout;
- todos os pacotes da janela são retransmitidos.

Esse comportamento caracteriza o protocolo Go-Back-N.

---

## Receptor

O receptor aceita apenas o pacote esperado.

Quando recebe:

- pacote correto → grava os dados e envia ACK;
- pacote fora de ordem → descarta o pacote e reenvi a o último ACK.

Também é realizada a simulação de perda artificial conforme a probabilidade informada no handshake.

---

## Encerramento

Após o envio de todos os dados:

- o emissor envia um pacote FIN;
- o receptor salva o arquivo;
- calcula o MD5;
- compara com o hash recebido no handshake;
- exibe as estatísticas.

---

# Compilação

Na raiz do projeto execute:

```powershell
javac -d bin src/protocol/*.java src/util/*.java src/sender/*.java src/receiver/*.java src/Emissor.java src/Receptor.java
```

---

# Execução

Primeiro execute o receptor:

```powershell
java -cp bin Receptor
```

Depois execute o emissor:

```powershell
java -cp bin Emissor <arquivo_origem> <IP_destino>:<arquivo_destino> <janela> <probabilidade_perda>
```

Exemplo:

```powershell
java -cp bin Emissor testes/entrada/arquivo_grande.txt 127.0.0.1:./testes/saida/arquivo_grande_recebido.txt 8 0.10
```

---

# Criando um arquivo grande para testes

PowerShell:

```powershell
1..5000 | ForEach-Object { "linha $_ - teste go-back-n" } > testes\entrada\arquivo_grande.txt
```

---

# Exemplo de saída

## Emissor

```
Handshake enviado.
ACK do handshake recebido.

Pacote DATA enviado. Seq = 0
ACK recebido. Ack = 0

Pacote DATA enviado. Seq = 1
ACK recebido. Ack = 1

...

TIMEOUT!
Retransmitindo de 32 até 39

...

Pacote FIN enviado.

===== ESTATÍSTICAS DO EMISSOR =====

Pacotes enviados: 515
Retransmissões: 224
ACKs recebidos: 487
Bytes enviados: 527164
Tempo total: 14242 ms
Throughput estimado: 37014.75 bytes/s
```

---

## Receptor

```
Handshake recebido.

Pacote DATA recebido em ordem. Seq = 0
ACK enviado.

Pacote perdido artificialmente. Seq = 45

Pacote fora de ordem descartado. Seq = 46

...

MD5 recebido:
2d4b....

Integridade verificada: arquivos idênticos.

===== ESTATÍSTICAS DO RECEPTOR =====

Pacotes DATA recebidos: 515
Perdas artificiais: 28
Pacotes fora de ordem descartados: 196
Taxa de perda efetiva: 5.44%
```

---

# Tecnologias Utilizadas

- Java
- DatagramSocket
- DatagramPacket
- ByteBuffer
- MD5 (MessageDigest)

---

# Autores

Felipe Correia Araújo
Luís Filipi Rosa dos Santos
Trabalho desenvolvido para a disciplina **Redes de Computadores** da **Universidade Federal de Alfenas (UNIFAL-MG)**.
/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "calculadora.h"
#include <stdio.h>
#include <stdlib.h>
#include <rpc/pmap_clnt.h>
#include <string.h>
#include <memory.h>
#include <sys/socket.h>
#include <netinet/in.h>

#ifndef SIG_PF
#define SIG_PF void(*)(int)
#endif

static int *
_suma_1 (inputs  *argp, struct svc_req *rqstp)
{
	return (suma_1_svc(*argp, rqstp));
}

static int *
_resta_1 (inputs  *argp, struct svc_req *rqstp)
{
	return (resta_1_svc(*argp, rqstp));
}

static int *
_multiplicacion_1 (inputs  *argp, struct svc_req *rqstp)
{
	return (multiplicacion_1_svc(*argp, rqstp));
}

static float *
_division_1 (inputs  *argp, struct svc_req *rqstp)
{
	return (division_1_svc(*argp, rqstp));
}

static void
calculadoraprog_1(struct svc_req *rqstp, register SVCXPRT *transp)
{
	union {
		inputs suma_1_arg;
		inputs resta_1_arg;
		inputs multiplicacion_1_arg;
		inputs division_1_arg;
	} argument;
	char *result;
	xdrproc_t _xdr_argument, _xdr_result;
	char *(*local)(char *, struct svc_req *);

	switch (rqstp->rq_proc) {
	case NULLPROC:
		(void) svc_sendreply (transp, (xdrproc_t) xdr_void, (char *)NULL);
		return;

	case SUMA:
		_xdr_argument = (xdrproc_t) xdr_inputs;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) _suma_1;
		break;

	case RESTA:
		_xdr_argument = (xdrproc_t) xdr_inputs;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) _resta_1;
		break;

	case MULTIPLICACION:
		_xdr_argument = (xdrproc_t) xdr_inputs;
		_xdr_result = (xdrproc_t) xdr_int;
		local = (char *(*)(char *, struct svc_req *)) _multiplicacion_1;
		break;

	case DIVISION:
		_xdr_argument = (xdrproc_t) xdr_inputs;
		_xdr_result = (xdrproc_t) xdr_float;
		local = (char *(*)(char *, struct svc_req *)) _division_1;
		break;

	default:
		svcerr_noproc (transp);
		return;
	}
	memset ((char *)&argument, 0, sizeof (argument));
	if (!svc_getargs (transp, (xdrproc_t) _xdr_argument, (caddr_t) &argument)) {
		svcerr_decode (transp);
		return;
	}
	result = (*local)((char *)&argument, rqstp);
	if (result != NULL && !svc_sendreply(transp, (xdrproc_t) _xdr_result, result)) {
		svcerr_systemerr (transp);
	}
	if (!svc_freeargs (transp, (xdrproc_t) _xdr_argument, (caddr_t) &argument)) {
		fprintf (stderr, "%s", "unable to free arguments");
		exit (1);
	}
	return;
}

int
main (int argc, char **argv)
{
	register SVCXPRT *transp;

	pmap_unset (CALCULADORAPROG, CALCULADORAVER);

	transp = svcudp_create(RPC_ANYSOCK);
	if (transp == NULL) {
		fprintf (stderr, "%s", "cannot create udp service.");
		exit(1);
	}
	if (!svc_register(transp, CALCULADORAPROG, CALCULADORAVER, calculadoraprog_1, IPPROTO_UDP)) {
		fprintf (stderr, "%s", "unable to register (CALCULADORAPROG, CALCULADORAVER, udp).");
		exit(1);
	}

	transp = svctcp_create(RPC_ANYSOCK, 0, 0);
	if (transp == NULL) {
		fprintf (stderr, "%s", "cannot create tcp service.");
		exit(1);
	}
	if (!svc_register(transp, CALCULADORAPROG, CALCULADORAVER, calculadoraprog_1, IPPROTO_TCP)) {
		fprintf (stderr, "%s", "unable to register (CALCULADORAPROG, CALCULADORAVER, tcp).");
		exit(1);
	}

	svc_run ();
	fprintf (stderr, "%s", "svc_run returned");
	exit (1);
	/* NOTREACHED */
}
